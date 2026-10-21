import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import BarraCobertura from './BarraCobertura'

describe('BarraCobertura', () => {
  it('mostra o percentual', () => {
    render(<BarraCobertura percentual={61} rotulo="Geral" />)
    expect(screen.getByText('Geral')).toBeInTheDocument()
    expect(screen.getByText('61%')).toBeInTheDocument()
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '61')
  })

  it('usa a faixa baixa abaixo de 50%', () => {
    const { container } = render(<BarraCobertura percentual={20} />)
    expect(container.querySelector('.barra')).toHaveClass('baixa')
  })

  it('usa a faixa média entre 50% e 80%', () => {
    const { container } = render(<BarraCobertura percentual={61} />)
    expect(container.querySelector('.barra')).toHaveClass('media')
  })
})
